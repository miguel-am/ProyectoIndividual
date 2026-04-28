using DesktopApp.Models;
using DesktopApp.Services;
using DesktopApp.ViewModels;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Controls;

namespace DesktopApp.Views.Reservation
{
    public partial class ListReservationView : UserControl
    {
        private readonly ApiClient _apiClient;
        private readonly ReservationListViewModel _vm = new ReservationListViewModel();


        public ListReservationView()
        {
            InitializeComponent();
            DataContext =  _vm;

        }

        private async void Eliminar_Reserva(object sender, RoutedEventArgs e)
        {
            var form = new DeleteCancelledReservationsView();
            form.Owner = Application.Current.MainWindow;
            var ok = form.ShowDialog();
            await _vm.CargarReservasAsync();
        }


        private async void Nueva_Reserva(object sender, RoutedEventArgs e)
        {

            var form = new AddReservationView();
            form.Owner = Application.Current.MainWindow;
            var ok = form.ShowDialog();
            await _vm.CargarReservasAsync();
        }
    }
}
