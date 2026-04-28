using DesktopApp.Models;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Windows;

namespace DesktopApp.Services
{
    public class ApiClient
    {
        private static ApiClient _instance;
        private readonly HttpClient _httpClient;
        private static string _token;

        private const string BASE_URL = "http://localhost:3000";

        public ApiClient()
        {
            _httpClient = new HttpClient();
            _httpClient.BaseAddress = new Uri(BASE_URL);
        }

        public static ApiClient Instance => _instance ??= new ApiClient();

        public void SetToken(string token)
        {
            _token = token;
        }

        private HttpRequestMessage CreateRequest(HttpMethod method, string url)
        {
            var request = new HttpRequestMessage(method, url);
            if (!string.IsNullOrEmpty(_token))
            {
                request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _token);
            }
            return request;
        }


        public async Task<List<Reservations>> GetReservasAsync()
        {
            var request = CreateRequest(HttpMethod.Get, $"reservations");
            var response = await _httpClient.SendAsync(request);

            if (!response.IsSuccessStatusCode)
                return new List<Reservations>();

            var json = await response.Content.ReadAsStringAsync();

            var bookings = JsonSerializer.Deserialize<List<Reservations>>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });

            return bookings ?? new List<Reservations>();
        }

        public async Task<bool> CancelReservationAsync(string reservationId)
        {
            try
            {
                var request = CreateRequest(HttpMethod.Patch, $"reservations/cancel/{reservationId}");
                var response = await _httpClient.SendAsync(request);

                // Depuración:
                if (!response.IsSuccessStatusCode)
                {
                    var contenido = await response.Content.ReadAsStringAsync();
                    MessageBox.Show($"Error API: {response.StatusCode}\n{contenido}");
                    return false;
                }

                return true;
            }
            catch (Exception)
            {
                throw;
            }
        }

        public async Task<List<Rooms>> GetRooms()
        {
            var response = await _httpClient.GetAsync("rooms");
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            var rooms = JsonSerializer.Deserialize<List<Rooms>>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true,
                Converters = { new JsonStringEnumConverter() }
            });
            return rooms;
        }

        public async Task<int> GetNextRoom(int numFloor)
        {
            var response = await _httpClient.GetAsync($"rooms/nextRoom/{numFloor}");
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
            {
                throw new Exception(json);
            }
            int newRoom = JsonSerializer.Deserialize<int>(json);
            return newRoom;
        }
        public async Task<string> PostRooms(int numFloor, string roomType, string description, int pricePerNight, int maxOccupancy, string availability,List<string> services)
        {
            var values = new Dictionary<string, string>()
                {
                    { "numFloor", numFloor.ToString()},
                    { "roomType",roomType.ToLower()},
                    { "description",description},
                    { "pricePerNight",pricePerNight.ToString()},
                    { "maxOccupancy",maxOccupancy.ToString()},
                    { "availability",availability.ToLower()},
                    { "services", JsonSerializer.Serialize(services ?? new List<string>()) }
                 };
            var content = new FormUrlEncodedContent(values);
            var response = await _httpClient.PostAsync("rooms/add", content);
            var body = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
                throw new Exception($"{(int)response.StatusCode} {response.ReasonPhrase}\n{body}");

            return body;
        }

        public async Task updateIdRoom(string id, string roomType, string description, int pricePerNight, int maxOccupancy, string availability,List<string> services)
        {
            var values = new Dictionary<string, string>()
                {
                    { "roomType",roomType.ToLower()},
                    { "description",description},
                    { "pricePerNight",pricePerNight.ToString()},
                    { "maxOccupancy",maxOccupancy.ToString()},
                    { "availability",availability.ToLower()},
                    { "services", JsonSerializer.Serialize(services ?? new List<string>()) }
                 };
            var content = new FormUrlEncodedContent(values);
            var response = await _httpClient.PatchAsync($"rooms/modify/{id}", content);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
                throw new Exception($"{(int)response.StatusCode} {response.ReasonPhrase}");

        }

        public async Task DeleteIdRoom(string id)
        {
            var response = await _httpClient.DeleteAsync($"rooms/delete/{id}");
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
            {
                throw new Exception(json);
            }
        }
        public async Task DeleteIdImgRoom(string id, string imagePath)
        {
            var request = new HttpRequestMessage(HttpMethod.Delete, $"rooms/delete/{id}/images")
            {
                Content = new StringContent(
                    JsonSerializer.Serialize(new { image = imagePath }),
                    Encoding.UTF8,
                    "application/json"
                )
            }
            ;
            var response = await _httpClient.SendAsync(request);

            response.EnsureSuccessStatusCode();
        }

        public async Task<Rooms> GetRoomsId(string id)
        {
            var response = await _httpClient.GetAsync($"rooms/{id}");
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();

            var rooms = JsonSerializer.Deserialize<Rooms>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });
            return rooms;
        }

        public async Task PostIdImgRoom(string id, List<string> filePaths)
        {
            using var form = new MultipartFormDataContent();

            foreach (var p in filePaths)
            {
                var bytes = await File.ReadAllBytesAsync(p);
                var content = new ByteArrayContent(bytes);

                var ext = Path.GetExtension(p).ToLowerInvariant();

                string type;

                switch (ext)
                {
                    case ".jpg":
                    case ".jpeg":
                        type = "image/jpeg";
                        break;

                    case ".png":
                        type = "image/png";
                        break;

                    case ".webp":
                        type = "image/webp";
                        break;

                    default:
                        type = "application/octet-stream";
                        break;
                }

                content.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(type);

                form.Add(content, "images", Path.GetFileName(p));
            }

            var response = await _httpClient.PostAsync($"rooms/add/{id}/images", form);
            response.EnsureSuccessStatusCode();

        }

        public async Task<List<Reviews>> GetReviewIdRoom(string id)
        {
            var response = await _httpClient.GetAsync($"reviews/room/{id}");
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            if (!response.IsSuccessStatusCode)
            {
                throw new Exception(json);
            }
            var reviews = JsonSerializer.Deserialize<List<Reviews>>(json, new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            });
            return reviews;
        }

        public async Task<string> PostReservationAsync(Reservations reserva)
        {
            var json = JsonSerializer.Serialize(reserva);
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await _httpClient.PostAsync("reservations/add", content);

            var respuestaJson = await response.Content.ReadAsStringAsync();

            if (response.IsSuccessStatusCode)
                return null;
            else
                return respuestaJson;
        }

        public async Task<List<User>> GetUsersByRolAsync(string rol)
        {
            var request = CreateRequest(HttpMethod.Get, $"/users/rol/{rol}");
            var response = await _httpClient.SendAsync(request);
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<List<User>>(json, new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
        }




        public async Task<string> LoginAsync(string email, string password)
        {
            var payload = new { email, password };
            var content = new StringContent(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");

            var response = await _httpClient.PostAsync("auth/login", content);
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);
            string token = doc.RootElement.GetProperty("token").GetString();
            string userRol = doc.RootElement.GetProperty("rol").GetString();
            if(userRol != "Usuario")
            {
                SetToken(token); // Guardamos solo la variable
                return token;
            }
            return null;
        }

        public async Task<User> GetUserByIdOrDniAsync(string searchData, string searchProperty)
        {
            if (string.IsNullOrEmpty(searchData) || string.IsNullOrEmpty(searchProperty))
                return null;

            try
            {
                var payload = new {searchData,searchProperty};
                var content = new StringContent(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");


                var response = await _httpClient.PostAsync("users/getOneUserByIdOrDni", content);

                if (response.IsSuccessStatusCode)
                {
                    var user = await response.Content.ReadFromJsonAsync<User>();
                    return user;
                }

                return null;
            }
            catch
            {
                return null;
            }
        }
        public async Task<bool> DeleteReservationAsync(string reservationId)
        {
            try
            {
                var request = CreateRequest(HttpMethod.Delete, $"reservations/delete/{reservationId}");
                var response = await _httpClient.SendAsync(request);
                if (!response.IsSuccessStatusCode)
                {
                    var contenido = await response.Content.ReadAsStringAsync();
                    MessageBox.Show($"Error API: {response.StatusCode}\n{contenido}");
                    return false;
                }
                return true;
            }
            catch (Exception)
            {
                throw;
            }
        }
        public void Logout()
        {
            _token = null;
        }



    }

}
