using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using DesktopApp.Commands;
using System.Windows.Input;
using DesktopApp.Views;
using DesktopApp.Views.Reservation;
using System.Windows;
using DesktopApp.Services;

namespace DesktopApp.ViewModels
{
    public class MainViewModel: INotifyPropertyChanged
    {
        private object? _currentView;
        public object? CurrentView
        {
            get => _currentView;
            set { 
                _currentView = value; 
                OnPropertyChanged(); 
            }
        }
        private string? _selectedMenu;
        public string? SelectedMenu
        {
            get => _selectedMenu;
            set
            {
                _selectedMenu = value;
                OnPropertyChanged();
            }
        }
        public ICommand NavigateCommand { get; }

        public MainViewModel()
        {
            SelectedMenu = "dashboard";
            CurrentView = new DashboardView();
            NavigateCommand = new RelayCommand(Navigation);

        }

        public void Navigation(object? p)
        {
            var key = p?.ToString()?.ToLower();

            if (SelectedMenu != key)
                SelectedMenu = key;

            switch (key)
            {
                case "dashboard":
                   CurrentView = new DashboardView();
                    break;
                case "users":
                    //CurrentView = new UsersView();
                    break;
                case "bookings":
                    CurrentView = new ListReservationView();
                    break;
                case "rooms":
                    CurrentView = new ListRoomsView();
                    break;
                case "power":
                    Logout();
                    break;
            }
        }
        private void Logout()
        {
            ApiClient.Instance.Logout();

            var currentMain = Application.Current.MainWindow;
            currentMain?.Hide();

            var login = new LoginView();
            if (currentMain != null)
                login.Owner = currentMain;

            bool? ok = login.ShowDialog();

            if (ok != true)
            {
                Application.Current.Shutdown();
                return;
            }

            var newMain = new MainWindow(skipLogin: true);
            Application.Current.MainWindow = newMain;
            newMain.Show();

            currentMain?.Close();
        }
       

        public event PropertyChangedEventHandler? PropertyChanged;
        private void OnPropertyChanged([CallerMemberName] string? n = null)
            => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(n));
    }
}
